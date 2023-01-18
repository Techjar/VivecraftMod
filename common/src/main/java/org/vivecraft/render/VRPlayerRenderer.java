package org.vivecraft.render;

import java.util.UUID;

import org.vivecraft.ClientDataHolder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.phys.Vec3;

public class VRPlayerRenderer extends PlayerRenderer
{
	static LayerDefinition VRLayerDef = LayerDefinition.create(VRPlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64);
	static LayerDefinition VRLayerDef_arms = LayerDefinition.create(VRPlayerModel_WithArms.createMesh(CubeDeformation.NONE, false), 64, 64);
    static LayerDefinition VRLayerDef_slim = LayerDefinition.create(VRPlayerModel.createMesh(CubeDeformation.NONE, true), 64, 64);
	static LayerDefinition VRLayerDef_arms_slim = LayerDefinition.create(VRPlayerModel_WithArms.createMesh(CubeDeformation.NONE, true), 64, 64);

    public VRPlayerRenderer(EntityRendererProvider.Context context, boolean slim, boolean seated) {
		super(context, slim);
        model = !slim ?
						(seated ?
                        new VRPlayerModel<>(VRLayerDef.bakeRoot(), slim) :
                        new VRPlayerModel_WithArms<>(VRLayerDef_arms.bakeRoot(), slim)) :
                    (seated ?
                        new VRPlayerModel<>(VRLayerDef_slim.bakeRoot(), slim) :
                        new VRPlayerModel_WithArms<>(VRLayerDef_arms_slim.bakeRoot(), slim));

        this.addLayer(new HMDLayer(this));
    }

    @Override
    public void render(AbstractClientPlayer entityIn, float pEntityYaw, float pPartialTicks, PoseStack matrixStackIn, MultiBufferSource pBuffer, int pPackedLight)
    {
        if (ClientDataHolder.getInstance().currentPass == RenderPass.GUI && entityIn.isLocalPlayer())
        {
            Matrix4f matrix4f = matrixStackIn.last().pose();
            double d0 = (new Vec3((double)matrix4f.m00, (double)matrix4f.m01, (double)matrix4f.m02)).length();
            matrixStackIn.last().pose().setIdentity();
            matrixStackIn.last().normal().setIdentity();
            matrixStackIn.translate(0.0D, 0.0D, 1000.0D);
            matrixStackIn.scale((float)d0, (float)d0, (float)d0);
            matrixStackIn.mulPose(Vector3f.ZP.rotationDegrees(180.0F));
            matrixStackIn.mulPose(Vector3f.YP.rotationDegrees(180.0F + ClientDataHolder.getInstance().vrPlayer.vrdata_world_pre.getBodyYaw()));
        }

        PlayerModelController.RotInfo playermodelcontroller$rotinfo = PlayerModelController.getInstance().getRotationsForPlayer(entityIn.getUUID());

        if (playermodelcontroller$rotinfo != null)
        {
            matrixStackIn.scale(playermodelcontroller$rotinfo.heightScale, playermodelcontroller$rotinfo.heightScale, playermodelcontroller$rotinfo.heightScale);
            super.render(entityIn, pEntityYaw, pPartialTicks, matrixStackIn, pBuffer, pPackedLight);
            matrixStackIn.scale(1.0F, 1.0F / playermodelcontroller$rotinfo.heightScale, 1.0F);
        }
    }

    @Override
    public Vec3 getRenderOffset(AbstractClientPlayer pEntity, float pPartialTicks)
    {
    	//idk why we do this anymore
        return pEntity.isVisuallySwimming() ? new Vec3(0.0D, -0.125D, 0.0D) : Vec3.ZERO;
       // return pEntity.isCrouching() ? new Vec3(0.0D, -0.125D, 0.0D) : super.getRenderOffset(pEntity, pPartialTicks);
    }

    @Override
    public void setModelProperties(AbstractClientPlayer pClientPlayer)
    {
        super.setModelProperties(pClientPlayer);

        VRPlayerModel<AbstractClientPlayer> playermodel = (VRPlayerModel<AbstractClientPlayer>) this.getModel();
        playermodel.crouching &= !pClientPlayer.isVisuallySwimming();
    }

    @Override
    protected void setupRotations(AbstractClientPlayer pEntityLiving, PoseStack pMatrixStack, float pAgeInTicks, float pRotationYaw, float pPartialTicks)
    {
    	UUID uuid = pEntityLiving.getUUID();
    	if (PlayerModelController.getInstance().isTracked(uuid))
    	{
    		PlayerModelController.RotInfo playermodelcontroller$rotinfo = PlayerModelController.getInstance().getRotationsForPlayer(uuid);
    		pRotationYaw = (float)Math.toDegrees(playermodelcontroller$rotinfo.getBodyYawRadians());
    	}

        //vanilla below here
            super.setupRotations(pEntityLiving, pMatrixStack, pAgeInTicks, pRotationYaw, pPartialTicks);
            }
}
